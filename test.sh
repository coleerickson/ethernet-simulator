rm utilization_data.txt
javac *.java

if [ $? == 0 ]; then
	for i in {1..30}
	do
		java -ea EthernetSimulator 1536 $i 10E6;
	done
fi
