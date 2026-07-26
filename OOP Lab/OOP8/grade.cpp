#include<iostream>
using namespace std;

class Student;

class Teacher
{
public:
    void calculateGrade(Student s);
};

class Student
{
    string name;
    int id;
    double marks[3];

public:
    Student(string n = "Unknown", int r = 0, double m1 = 0, double m2 = 0, double m3 = 0): name(n),id(r), marks{m1, m2, m3} {}

    friend class Teacher;
};

void Teacher::calculateGrade(Student s)
{
    int n = sizeof(s.marks)/ sizeof(s.marks[0]);
    double total = 0.0;
    for(int i = 0; i < n; i++)
    {

        total += s.marks[i];
    }
    double avg = total/ n;

    cout << "\nStudent Name: " << s.name << "(Roll: " << s.id << ")" << endl;
    cout << "TOtal Marks: " << total << endl;
    cout << "Average Marks: " << avg << endl;

    bool fail = false;
    for(int i = 0; i < n; i++)
    {
        if(s.marks[i] < 30)
            fail = true;
    }

    if(fail == true)
        cout << "Failed" << endl;
    else if(avg >= 80)
        cout << "A+" << endl;
    else if ( avg >= 70)
        cout << "A" << endl;
    else if( avg >= 60)
        cout << "B" << endl;
    else if (avg >= 50)
        cout << "B-" << endl;
    else if (avg >= 40)
        cout << "C" << endl;
    else
        cout << "Failed" << endl;
}

int main()
{
    Student s1("Rahim", 80, 65, 25, 30);

    Teacher t;
    t.calculateGrade(s1);

    return 0;
}
