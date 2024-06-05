cd DemoApi && mvn clean install -DskipTests=true -Dmaven.test.skip=true -U
cd ..
cd extendlib && mvn clean install -DskipTests=true -Dmaven.test.skip=true -U 
cd ..
cd extendDemoLib && mvn clean install -DskipTests=true -Dmaven.test.skip=true -U 
cd ..
cd extendDemoApp && mvn clean package -DskipTests=true -Dmaven.test.skip=true -U 
cd ..