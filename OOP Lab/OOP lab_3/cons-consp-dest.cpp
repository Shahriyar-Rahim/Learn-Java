#include<iostream>
using namespace std;

class Example
{
    int i;
    string name;

public:
    Example();
    Example(int a, string b);
    void display();
    ~Example();
};

Example::Example()
{
    cout << "Default constructor called" << endl;
}

Example::Example(int x, string y)
{
    cout <<"Parameterized constructor called and taking input" << endl;
    i = x;
    name = y;
}

void Example::display()
{
    cout << "Id: " << i << endl << "Name: " << name << endl;;
}

Example::~Example()
{
    cout << "Destructor called" << endl;
}

int main()
{
    Example();
    Example e1(11, "XXX");
    e1.display();

    return 0;
}



