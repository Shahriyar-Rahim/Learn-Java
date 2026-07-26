#include<bits/stdc++.h>
using namespace std;



int main(){
    int id;
    string name;
    float cgpa;

    
    cout << "Eter your id ";
    cin >>id;
    cout << "Eter your name ";
    cout << "Eter your cgpa ";
    cin >>cgpa;

    cout << "Your name " << name << endl<< "your id " << id <<  endl << "Your cg " << cgpa;

    

    cout << "Eter your id ";
    cin >>id;

    cin.ignore();
    cout << "Eter your name ";
    getline(cin, name);
    cout << "Eter your cgpa ";
    cin >>cgpa;

    cout << "Your name " << name << endl<< "your id " << id <<  endl << "Your cg " << cgpa;

return 0;
}
