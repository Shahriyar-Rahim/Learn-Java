#include<bits/stdc++.h>
using namespace std;

class Student {
private:
    int id;
    string name;
    double cgpa;


    //validate user input
    template<typename T>
    T getValidInputFromUser(const string& prompt){
     T value;
        while(true) {
            cout<< prompt;
            if(!(cin >> value)) {
                cout<< "Invalid Input. Please try again" << endl;
                cin.clear();  //clear errors
                //ignore the rest of the line to prevent infinite loop
                cin.ignore(numeric_limits<streamsize>::max(), '\n');
            }
            else {
                    //clear the nuffer
                cin.ignore(numeric_limits<streamsize>::max(), '\n');
                return value;
            }
        }
    }


public:
    //constructor
    Student () : id(0), name("N/A"), cgpa(0.0f) {}

    //take input and validate data
    void inputData() {
        while(true){
            id = getValidInputFromUser<int>("Enter your ID: ");
            if(id >= 0) {
                break;
            }
            cout << "ID cannot be negative. Please try again." << endl;
        }

        cout << "Enter your name: ";
        getline(cin, name);
        if (name.empty()){
            name = "N/A";
        }

        while(true) {
            cgpa = getValidInputFromUser<double>("Enter your CGPA (0.0 - 4.0): ");
            if(cgpa >= 0.0f && cgpa <= 4.0f){
                break;
            }
            cout << "CGPA must be between 0.0 and 4.0. Please try again." << endl;
        }
    }

    void displayData() const {
        cout<< "\n+-------------------------+" << endl;
        cout << "| --- Student Details --- |" << endl;
        cout << "+-------------------------+" << endl;
        cout << "| Name: " << left << setw(16) << name << "|" << endl;
        cout << "| ID:   " << left << setw(16) << id << "|" << endl;
        cout << "| CGPA: " << left << setw(16) << fixed << setprecision(2) << cgpa << "|" << endl;
        cout << "+-------------------------+" << endl;

    }
};


int main(){
    //handle runtime errors
    try{
    Student s1, s2;
    s1.inputData();
    s2.inputData();
    s1.displayData();
    s2.displayData();
    }
    catch (const exception& e) {
        cerr << "An Error Occured: " << e.what() << endl;
        return 1;
    }
return 0;
}
