rm utilization_data.txt
javac *.java

if [ $? == 0 ]; then
	for j in 64 128 256 512 768 1536 3072
	do
		for i in {1..30}
		do
			java -ea EthernetSimulator $j $i 10E6;
		done
	done
fi
