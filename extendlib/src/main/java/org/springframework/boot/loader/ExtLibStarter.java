package org.springframework.boot.loader;

import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.JarFileArchive;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ExtLibStarter extends JarLauncher {
    private static final String FAIL_ON_ERROR = "failOnError";
    private static final String FALSE = "false";
    private static final String TRUE = "true";
    private static final String SUFFIX = ".jar";
    private static final String CACHE_PREFIX = ".cache";
    private static final String INCLUDE_JARS="includejars";
    private static final String EXCLUDE_JARS="excludejars";

    private static final String OSName="os.name";

    private static final String systemCode = "system.code";
    private static final String modelCode = "model.code";
    private static final String decode = "UTF-8";
    public ExtLibStarter(){

    }
    public ExtLibStarter(Archive archive) {
        super(archive);
    }

    @Override
    protected boolean isNestedArchive(Archive.Entry entry) {
        return false;
    }

    @Override
    protected void postProcessClassPathArchives(List<Archive> archives) throws Exception {
        try {
            File[] cfs = preCheck();
            if (cfs == null) {
                println("启动参数未指定外挂目录[externalLibPath] 或 外挂目录下没有目标文件，相关功能不会开启！");
                return;
            }

            List<Archive> extArchives = new ArrayList<>();
            //收集外挂包
            collectExternalArchives(cfs, extArchives);
            //检查外挂包是否存在版本冲突的jar包
            List<String> extArtifacts = checkExternalArchives(extArchives);
            //移除已经加载的但与外挂的版本不同或相同的jar包
            removeLoadedArchives(archives, extArtifacts);
            // 添加外挂包到classpath
            for (Archive archive : extArchives) {
                archives.add(0, archive);
                println("++++++++++++添加外挂包：" + archive.getUrl().getFile());
            }
        } catch (Throwable e) {
            if (TRUE.equals(System.getProperty(FAIL_ON_ERROR, TRUE).toLowerCase())) {
                throw e;
            } else {
                println("处理外挂包出现异常,启动继续！:"+e.getMessage());
            }
        }
    }// end
    private void filter(List<Archive> extArchives, String includeJars, String excludeJars) throws MalformedURLException {
        List<Archive> filtered = new ArrayList<>();
        if(!isEmpty(excludeJars)){
            for(Archive archive : extArchives){
                String jarName = extractedArtifact(archive);
                if(excludeJars.contains(jarName)){
                    filtered.add(archive);
                }
            }
        }else if(!isEmpty(includeJars)){
            for(Archive archive : extArchives){
                String jarName = extractedArtifact(archive);
                if(!includeJars.contains(jarName)){
                    filtered.add(archive);
                }
            }
        }
        if(!filtered.isEmpty()){
            println("===================被过滤的外挂jar包===============:");
            for(Archive archive : filtered){
                extArchives.remove(archive);
                println("--------:"+archive.getUrl().getFile());
            }
        }
    }

    private boolean isEmpty(String s){
        if(s == null || s.trim().length() == 0){
            return true;
        }
        return false;
    }
    /**
     * @return
     */
    private File[] preCheck() throws Exception {
        String externalLibPath = System.getProperty("externalLibPath");
        println("************externalLibPath：" + externalLibPath);
        if (externalLibPath == null || externalLibPath.trim().length() == 0) {
            return null;
        }
        List<File> dirs = extractedFiles(externalLibPath);

        if (dirs.size() <= 0) {
            return null;
        }
        return dirs.toArray(new File[0]);
    }

    /**
     * @param externalLibPath
     * @return
     */
    private List<File> extractedFiles(String externalLibPath) throws Exception {
        List<String> extPaths = new ArrayList<>();
        if (externalLibPath.contains(",")) {
            String[] split = externalLibPath.split(",");
            for (String path : split) {
                if (path != null && path.trim().length() > 0) {
                    extPaths.add(path);
                }
            }
        } else {
            extPaths.add(externalLibPath);
        }

        File cacheDir = null;
        try {
            cacheDir = generateCaches();
        } catch (Exception e) {
            println("处理外挂caches出现异常，原因：" + e.getMessage());
            throw new RuntimeException("处理外挂caches出现异常",e);
        }

        return copyExtFile(extPaths, cacheDir);
    }

    private List<File> copyExtFile(List<String> extPaths, File cacheDir) throws Exception {
        List<File> dirs = new ArrayList<>();
        for (String path : extPaths) {
            File extDir = new File(path);
            if (!extDir.exists()) {
                println("指定的文件路径不存在，[" + path + "]！");
                continue;
            }
            if (!extDir.isDirectory()) {
                throw new IllegalArgumentException("The path [" + path + "] must be a directory!");
            }
            copyExtFilesCache(extDir, cacheDir);
        }
        File[] jars = cacheDir.listFiles();
        if (jars != null && jars.length > 0) {
            dirs.addAll(Arrays.asList(jars));
        }
        return dirs;
    }

    private boolean processActive(String processId) {
        String[] command;
        String os = System.getProperty(OSName).toLowerCase();
        if (isWindows(os)) {
            command = new String[]{"cmd", "/c", "tasklist /FI \"PID eq " + processId + "\""};
        } else if (isLinuxOrUnix(os)) {
            command = new String[]{"/bin/sh", "-c", "ps aux|awk '{print $2}'|grep -w " + processId};
        } else {
            return true;
        }
        Process exec;
        try {
            exec = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            println("-------------:检查进程号对应的进程是否存在出错，原因:" + e.getMessage());
            return true;
        }
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            boolean ret = content.toString().contains(processId);
            println("-------------进程ID[" + processId + "] 对应的进程是否存在:" + ret + ". command:" +
                    Arrays.toString(command));
            return ret;
        } catch (IOException e) {
            println("-------------:检查进程号对应的进程是否存在出错，原因:" + e.getMessage());
            return true;
        }
    }

    private boolean isLinuxOrUnix(String os) {
        return os.contains("linux") || os.contains("sunos");
    }

    private boolean isWindows(String os) {
        return os.contains("windows");
    }

    private File generateCaches() throws Exception {
        String appJarPath = getAppJarLocation();

        File appJar = new File(appJarPath);
        String sysCode = System.getProperty(systemCode);
        String deployModuleCode = System.getProperty(modelCode);
        String temp = CACHE_PREFIX;
        if (null != sysCode && sysCode.trim().length() > 0) {
            temp += "_" + sysCode.trim();
        }
        if (null != deployModuleCode && deployModuleCode.trim().length() > 0) {
            temp += "_" + deployModuleCode.trim();
        }
        if (temp.equals(CACHE_PREFIX)) {
            temp += "_" + appJar.getName().substring(0, appJar.getName().contains(".") ?
                    appJar.getName().lastIndexOf(".") : appJar.getName().length());
        }

        File appCacheDir = new File(appJar.getParentFile(), temp);
        if (!appCacheDir.exists()) {
            appCacheDir.mkdirs();
        } else {
            cleanUnActiveProcessDirs(appCacheDir);
        }

        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        File processDir = new File(appCacheDir, pid);
        if (!processDir.exists()) {
            processDir.mkdirs();
        }
        return processDir;
    }

    private String getAppJarLocation() {
        URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        String appJarPath = null;
        try {
            appJarPath = java.net.URLDecoder.decode(url.getPath(), decode);
        } catch (Exception e) {
            appJarPath = url.getPath();
        }
        return appJarPath;
    }

    //清理不活动的的进程的外挂缓存目录
    private void cleanUnActiveProcessDirs(File appCacheDir) throws Exception {
        File[] processDirs = appCacheDir.listFiles();
        if (processDirs != null) {
            for (File processDir : processDirs) {
                if (!processActive(processDir.getName())) {
                    try{
                        FileTools.deleteDirectoy(processDir);
                    }catch (Exception e){
                        String mesg = "清理目录["+processDir.getAbsolutePath()+"]失败,请手工删除!";
                        throw new RuntimeException(mesg,e);
                    }
                }
            }
        }

    }

    private void copyExtFilesCache(File srcDir, File destDir) throws IOException {
        copyFiles(srcDir, destDir);
    }

    private void copyFiles(File extFile, File cache) throws IOException {
        File[] files = extFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(SUFFIX);
            }
        });
        if (files == null) {
            return;
        }
        for (File file : files) {
            File destFile = new File(cache, file.getName());
            FileTools.doCopyFile(file, destFile, true);
        }
    }

    private void clear(File cache) {
        File[] files = cache.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (File file : files) {
            if (!file.delete()) {
                println("---------:文件删除失败:" + file.getAbsolutePath());
            }
        }

    }

    /**
     * @param cfs
     * @param extArchives
     */
    private void collectExternalArchives(File[] cfs, List<Archive> extArchives) throws Exception {
        List<File> jarInJars = new ArrayList<File>();
        List<File> normalJar = new ArrayList<File>();

        //分别获取dist中的jar存入jarInJars，非dist的jar存入normalJars
        filter(cfs, jarInJars, normalJar);
        //将jarInJars即dist中的jar加入到外挂包extArchives中
        addJarInJarToArchives(jarInJars, extArchives);
        //移除dist中已存在的normal jar的包
        removeInDistArchives(extArchives, normalJar);
        //将normalJar加入到外挂包extArchives中
        addNormalJarToArchives(normalJar, extArchives);
        //根据参数进行外挂包过滤
        filter(extArchives,System.getProperty(INCLUDE_JARS),System.getProperty(EXCLUDE_JARS));
    }

    /**
     * @param extArchives
     * @return
     */
    private List<String> checkExternalArchives(List<Archive> extArchives) throws MalformedURLException {
        Map<String, Set<String>> multiValuedMap = new HashMap<>();

        List<String> extArtifacts = new ArrayList<>();
        for (Archive archive : extArchives) {
            String archiveFile = extractedArtifact(archive);
            String[] segs = archiveFile.split("-\\d");
            extractedMultiValueMap(multiValuedMap, archive.getUrl().getFile(), segs);
            extArtifacts.add(segs[0]);
        }

        boolean flag = false;
        Iterator<Map.Entry<String, Set<String>>> iterator = multiValuedMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Set<String>> entry = iterator.next();
            if (entry.getValue().size() == 1) {
                continue;
            }
            Set<String> collect = entry.getValue().stream().map((url) -> {
                String archiveFile = extractedUrl(url);
                return archiveFile;
            }).collect(Collectors.toSet());

            if (collect.size() > 1) {
                flag = true;
                println("=========================外挂包中存在版本不一致的jar包：");
                entry.getValue().forEach((archive) -> {
                    print(archive + "   ");
                });
                println("");
            }
        }

        if (flag) {
            throw new IllegalArgumentException("外挂包中存在版本不一致的jar包，请检查！");
        }
        return extArtifacts;
    }

    /**
     * @param multiValuedMap
     * @param archive
     * @param segs
     */
    private void extractedMultiValueMap(Map<String, Set<String>> multiValuedMap, String archive, String[] segs) {
        multiValuedMap.computeIfAbsent(segs[0], k -> new HashSet<>());
        multiValuedMap.get(segs[0]).add(archive);
    }

    /**
     * @param archives
     * @param extArtifacts
     * @throws MalformedURLException
     */
    private void removeLoadedArchives(List<Archive> archives, List<String> extArtifacts) throws MalformedURLException {
        List<Archive> removing = new ArrayList<>();
        for (Archive archive : archives) {
            String archiveFile = extractedArtifact(archive);
            if (extArtifacts.contains(archiveFile.split("-\\d")[0])) {
                removing.add(archive);
            }
        }
        for (Archive arc : removing) {
            println("------------被移除的Archive：" + arc.getUrl().getFile());
        }
        archives.removeAll(removing);
    }

    /**
     * @param distArchives
     * @param normalFiles
     * @throws MalformedURLException
     */
    private void removeInDistArchives(List<Archive> distArchives, List<File> normalFiles) throws IOException {
        List<Archive> removing = new ArrayList<>();
        for (Archive archive : distArchives) {
            String archiveFile = extractedArtifact(archive);
            for (File normalArchive : normalFiles) {
                String normalArchiveFile = extractedFile(normalArchive);
                if (archiveFile.split("-\\d")[0].equals(normalArchiveFile.split("-\\d")[0])) {
                    removing.add(archive);
                    break;
                }
            }
        }
        for (Archive arc : removing) {
            println("------------被移除的Archive：" + arc.getUrl().getFile());
        }
        distArchives.removeAll(removing);
    }

    /**
     * @param archive
     * @return
     * @throws MalformedURLException
     */
    private String extractedArtifact(Archive archive) throws MalformedURLException {
        String archiveFile = archive.getUrl().getFile();
        return extractedUrl(archiveFile);
    }

    /**
     * @param file
     * @return
     * @throws MalformedURLException
     */
    private String extractedFile(File file) throws IOException {
        return extractedArtifact(new JarFileArchive(file));
    }

    /**
     * @param archiveFile
     * @return
     */
    private String extractedUrl(String archiveFile) {
        archiveFile = archiveFile.replace("\\", "/");
        if (archiveFile.endsWith("!/")) {
            archiveFile = archiveFile.substring(0, archiveFile.length() - 2);
            archiveFile = archiveFile.substring(archiveFile.lastIndexOf("/") + 1);
        } else {
            archiveFile = archiveFile.substring(archiveFile.lastIndexOf("/") + 1);
        }
        return archiveFile;
    }

    private void filter(File[] cfs, List<File> jarInJars, List<File> normalJar) throws IOException {
        for (File cf : cfs) {
            if (jarInJar(cf)) {
                jarInJars.add(cf);
            } else {
                normalJar.add(cf);
            }
        }
    }

    public void addJarInJarToArchives(List<File> jarInJars, List<Archive> archives) throws IOException {
        for (File jar : jarInJars) {
            addJarInJar(jar, archives);
        }
    }

    public void addNormalJarToArchives(List<File> normalJar, List<Archive> archives) throws IOException {
        for (File jarFile : normalJar) {
            archives.add(new JarFileArchive(jarFile));
        }
    }

    private void addJarInJar(File extFile, List<Archive> archives) throws IOException {
        JarFileArchive root = null;
        try {
            root = new JarFileArchive(extFile);
            List<Archive> nestArchives = root.getNestedArchives(new Archive.EntryFilter() {
                @Override
                public boolean matches(Archive.Entry entry) {
                    return entry.getName().endsWith(SUFFIX);
                }
            });
            for (Archive a : nestArchives) {
                archives.add(a);
            }
        } finally {
            if (root != null) {
                try {
                    root.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private boolean jarInJar(File file) throws IOException {
        JarFile jf = null;
        try {
            jf = new JarFile(file);
            Enumeration<JarEntry> entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(SUFFIX)) {
                    return true;
                }
            }
        } finally {
            closeQuiet(jf);
        }
        return false;
    }

    private void closeQuiet(Closeable file) {
        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
                println("ERROR: Failed to run closeQuiet. " + e.getMessage());
            }
        }
    }
    private void println(String message) {
        System.out.println(message);
    }
    private void print(String message) {
        System.out.print(message);
    }
    public static void main(String[] args) throws Exception {
        new ExtLibStarter().launch(args);
    }
}
