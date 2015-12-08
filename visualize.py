import matplotlib.pyplot as plt

numHost = []
utilizations = []

with open("utilization_data.txt","r") as f:
    for line in f:
        numHosts, utilization = map(float, line.split())
        numHost.append(numHosts)
        utilizations.append(utilization)

plt.xlim(0,30)
plt.ylim(0,10)
plt.xlabel("Number of hosts")
plt.ylabel("utilization")
plt.plot(numHost,utilizations)
plt.show()
