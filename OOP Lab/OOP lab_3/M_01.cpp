#include <iostream>
#include <string>
using namespace std;

class Student {
    int id;
    string name;

public:
    Student(int i, string n);
    void display();
};

Student::Student(int i, string n) {
    id = i;
    name = n;
}

void Student::display() {
    cout << "ID: " << id << " Name: " << name << endl;
}

int main() {
    Student s1(101, "Rahim");
    s1.display();
    return 0;
}
