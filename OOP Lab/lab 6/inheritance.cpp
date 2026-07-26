#include<iostream>
using namespace std;

class A
{
public:
    string x = "Hi";
    A()
    {
        cout<< "A" << endl;
    }
};


class B : virtual public A
{
public:
    B()
    {
        cout << "B" << endl;
    }


};


class C : virtual public A
{
public:
    C()
    {
        cout << "C" << endl;
    }
};

class D : virtual public B, public C
{
public:
    D()
    {
        cout << "D" << endl << x << endl;
    }
};


int main()
{
    D obj;

    obj.x = "How are you A";

    cout << obj.x << endl;

            return 0;
}
