rm build/* -f
rm wro/* -f
mkdir build
java -jar wro4j-runner-1.3.8.1-jar-with-dependencies.jar -m -c yuiCssMin --targetGroups all
cp wro/*.css build
java -jar wro4j-runner-1.3.8.1-jar-with-dependencies.jar -m -c googleClosureSimple --targetGroups all
cp wro/*.js build
rm wro -R
mv build wro
