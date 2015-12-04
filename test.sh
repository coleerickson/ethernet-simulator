javac *.java

if [ $? == 0 ]; then
	java -ea EthernetSimulator;
fi
