import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import os 


filename = os.path.join(os.path.dirname(os.getcwd()), "bverify_benchmark/jmh-result.csv")
data = pd.read_csv(filename)


x   = data['Score']
y   = np.arange(len(data['Benchmark']))
err = data['Score Error (99.9%)']
labels = []
for name in data['Benchmark']:
  labels.append(name[23:])

plt.rcdefaults()
plt.barh(y, x, xerr=err, color='blue', ecolor='red', alpha=0.4, 
align='center')
plt.yticks(y, labels)
plt.xlabel("Performance (ms/op)")
plt.title("Throughput Benchmark (on b_verify with 10k records)")
plt.show()
