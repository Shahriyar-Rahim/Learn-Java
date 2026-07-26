#include<iostream>
using namespace std;

class Person
{
protected:
    string name;
    int age;

public:
    Person(string n = "un", int a = 0) : name(n), age(a)
    {
        cout << "Default constructor called" << endl;
        cout << "Name: " << name << endl;
        cout << "Age: " << age << endl;
    }

    Person(string n, int a)
    {
        name = n;
        age = a;
    }
};

class Teacher: virtual public Person
{
protected:
    int employeeId;

public:
    Teacher(string n, int a, int e)
    {
        cout << "Teacher constructor called" << endl;
    }

    void showTeacher()
    {
        cout << "Employee Id: " << employeeId << endl;
    }
};

class Research
{
protected:
    string resar;

public:
    Research(string n, int a, string r = "un")
    {
        cout << "Reasearch constructor called" << endl;
    }

    void showResearch()
    {
        cout << "Reasearch Area: " << resar << endl;
    }
};


class Professor: virtual public Teacher, public Research
{
public:
    Professor(string n, int a, int e, string ra) : Person(n, a), Teacher(n,a,e), Research(n, a, r)
    {
        cout << "Professor constructor called" << endl;
    }

    void showProfessor()
    {

    }
};
