#include<iostream>
using namespace std;

class Person
{
protected:
    string name;
    int age;

public:
    Person(string x = "Unknown", int a = 0) : name(x), age(a)
    {
        cout << "Person constructor is called" << endl;
    }

    void showPerson()
    {
        cout << "Name: " << name << endl << "Age: " << age << endl;
    }
};


class Teacher : virtual public Person
{
protected:
    int employeeId;

public:
    Teacher(string s, int r, int q = 0) : Person(s, r), employeeId(q)
    {
        cout << "Teacher constructor is called" << endl;
    }

    void showTeacher()
    {
        cout << "Employee ID: " << employeeId << endl;
    }
};

class Researcher : virtual public Person
{
protected:
    string researchArea;

public:
    Researcher(string g, int k, string l = "Unknown") : Person(g, k), researchArea(l)
    {
        cout << "Researcher constructor is called" << endl;
    }

    void showResearch()
    {
        cout << "Research Area: " << researchArea << endl;
    }
};

class Professor : virtual public Teacher, public Researcher
{
public:
    Professor(string p, int q, int r, string s = "Unknown") : Person(p, q), Teacher(p, q, r), Researcher(p, q, s)
    {
        cout << "Professor constructor called" << endl << endl;
    }

    void showProfessor()
    {
        showPerson();
        showTeacher();
        showResearch();
    }
};

int main()
{
    Professor obj("Rahim", 21, 3498, "BI");

    obj.showProfessor();

    return 0;
}
