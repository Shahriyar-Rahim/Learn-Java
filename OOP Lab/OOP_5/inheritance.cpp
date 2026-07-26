#include<iostream>
using namespace std;

class Person
{
protected:
    string name;
    int age;

public:
    Person(string x, int y)
    {
        name = x;
        age = y;
        cout << "Person constructor called(default)" << endl;
    }

    void displayPerson()
    {
        cout << "Name: " << name << endl;
        cout << "Age: " << age << endl;
    }
};

class Student: public Person
{
    int id;
    double cgpa;

public:
    Student(string n, int a, int i, double c) : Person (n, a)
    {
        id = i;
        cgpa = c;
        cout << "Student constructor called" << endl;
    }

    void displayStudent()
    {
        displayPerson();
        cout << "ID: " << id << endl;
        cout << "CGPA: " << cgpa << endl;
    }
};

int main()
{
    Student s1("Shahriyra", 21, 59, 3.72);

    s1.displayStudent();

    return 0;
}
