#include <bits/stdc++.h>
using namespace std;

int main(){
      vector<int> marks = { 72, 88, 45, 32, 32, 79, 90};

      sort(marks.begin(), marks.end());

      set<int> uniq(marks.begin(), marks.end());

      map<int, int> freq;
      for(int m: marks) freq[m]++;

      cout << "Highest: " << marks.back() << endl;
      cout << "Unique: " << uniq.size() << endl;
      cout << "Frequency: " << freq[32] << endl;
}