#include<bits/stdc++.h>
#include <iomanip> // Required for formatting
#include <vector> // Required for vector

using namespace std;

class Student {
private:
    int id;
    string name;
    double cgpa;

public:
    void inputName(){
        cout << "Eter your id ";
        cin >>id;

        cin.ignore();
        cout << "Eter your name ";
        getline(cin, name);
        cout << "Eter your cgpa ";
        cin >>cgpa;
    }

    void displayData() {
        cout << "\n+-------------------------+" << endl;
        cout << "| --- Student Details --- |" << endl;
        cout << "+-------------------------+" << endl;
        cout << "| Name: " << left << setw(16) << name << "|" << endl;
        cout << "| ID:   " << left << setw(16) << id << "|" << endl;
        cout << "| CGPA: " << left << setw(16) << fixed << setprecision(2) << cgpa << "|" << endl;
        cout << "+-------------------------+" << endl;
    }

};


int main(){

    Student s1, s2;
    s1.inputName();
    s1.displayData();
    s2.inputName();
    s2.displayData();


return 0;
}
