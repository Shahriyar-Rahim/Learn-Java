#include<iostream>
using namespace std;

class Student {
    string name;
    int age;
    float cgpa;

public:
    Student();
    Student(string x);
    Student(string x, int y, float z);
    void display();
    ~Student();
};

Student::Student()
{
    cout << "From default constructor" << endl;
    name = "Unknown";
    age = 0;
    cgpa = 0.0;
}

Student::Student(string p)
{
    cout << "Enter your name(one parameter): ";
    getline(cin, p);
    if(p.empty())
    {
        cout << endl << "Invalid name (from one parameter)! Setting default name" << endl;
        name = "Unknown";
    }
    else {
        name = p;
        age = 0;
        cgpa = 0.0;
    }
}

Student::Student(string a, int b, float c)
{
    cout << "\nFor three parameter\n";
    cout << "Enter your name: ";
    getline(cin, a);
    cout << "Enter your age: ";
    cin >> b;
    cout << "Enter your CGPA: ";
    cin >> c;

    cin.ignore(1000, '\n');

    if(a.empty() || ( b < 0) )
    {
        cout << endl<< "Invalid input (from three parameter)! Setting default value to all the field" << endl;
        name = "Unknown";
        age = 0;
        cgpa = 0.0;
    }
    else {
        name = a;
        age = b;
    }
    if( c < 0 || c > 4.0)
    {
        cout << endl << "Invalid CGPA (from three parameter)! Setting default value 0.0 to CGPA" << endl;
        cgpa = 0.0;
    }
    else {
        cgpa = c;
    }
}

void Student::display()
{
    cout << endl << "Name: " << name << endl << "Age: " << age << endl << "CGPA: " << cgpa << endl;
}


Student::~Student()
{
    cout << endl << "Clearing memory for: " << name << endl;
}


int main()
{
    Student s1;
    Student s2(" ");
    Student s3(" ", 0, 0.0);
    s1.display();
    s2.display();
    s3.display();

    return 0;
}

