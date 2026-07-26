#include <iostream>
using namespace std;

class Employee {
private:
    int id;
    double salary;

public:
    //setter function also a validation of non-negative number
    void setSData(int i, double d){
        if(i <= 0 || d <= 0){
            throw invalid_argument("Invalid Input");
        } else {
            id = i;
            salary = d;
        }
    }

    //getter function returns a value type integer
    int getId(){
        return id;
    }

    double getSalary() {
        return salary;
    }
};

int main() {
    Employee e1; // making a object named s1
    e1.setSData(1, 3434524); //seting value
    cout<<"Your id: "<< e1.getId() << endl; //getting value
    cout<<"Your salary: "<< e1.getSalary() << endl;

    return 0;
}
