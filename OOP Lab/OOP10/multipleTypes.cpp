#include<iostream>
using namespace std;

template <class t, class t2, class t3>
class Student
{
    t age;
    t2 roll;
    t3 cgpa;

public:
    Student(t a, t2 r, t3 c) : age(a), roll(r), cgpa(c) {}
    void display()
    {
        cout << "Age: " << age << endl;
        cout << "Roll: " << roll << endl;
        cout << "CGPA: " << cgpa<< endl;

    }
};


int main()
{
    Student<int, long long , float> s1(25, 802520205101059, 3.72);
    s1.display();
}
