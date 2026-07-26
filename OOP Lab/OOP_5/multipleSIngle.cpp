#include<iostream>
using namespace std;

class A
{
public:
    A()
    {
        cout << "COnstrutor called from A" << endl;
    }
};

class C
{
public:
    C()
    {
        cout << "Constructor called from C" << endl;
    }
};

class B : public A
{
public:
    B()
    {
        cout << "Constructor called from B" << endl;
    }
};

class D : public C
{
public:
    D()
    {
        cout << "COnstructor called from D" << endl;
    }
};

int main()
{
    B a;
    D c;

    return 0;
}
