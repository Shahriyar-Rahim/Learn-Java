#include <iostream>
#include<stdexcept>
using namespace std;

class Student {
private:
    int id;

public:
    //setter function also a validation of non-negative number
    void setId(){
        if(cin >> id && id <= 0){
            throw invalid_argument("ID must be positive and a number");
        } else {
            id;
        }
    }

    //getter function returns a value type integer
    int getId(){
        return id;
    }
};

int main() {
    Student s1; // making a object named s1

    try{
        s1.setId();
        cout<<"Your id: "<< s1.getId() << endl;
    }
    catch (const invalid_argument& e) {
        cerr << "Error: " << e.what();
    }

    return 0;
}
