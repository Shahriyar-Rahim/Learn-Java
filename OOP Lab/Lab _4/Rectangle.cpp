#include<iostream>
using namespace std;

class Rectangle {
    double length, width;

public:
    Rectangle();
    Rectangle(double x);
    Rectangle(double x, double y);
    double Area();
    void display();
    ~Rectangle();
};

Rectangle::Rectangle()
{
    cout << "From default constructor" << endl;
    length = 1;
    width = 1;
}

Rectangle::Rectangle(double p)
{
    cout << "Enter the length(one parameter): ";
    cin >> p;
    if(p < 0)
    {
        cout << endl << "Invalid length (from one parameter)! Setting default to 0" << endl;
        length = 1;
    }
    else {
        length = p;
        width = p;
    }
}

Rectangle::Rectangle(double a, double b)
{
    cout << "Enter the length(two parameter): " << endl;
    cout << "Enter Length: ";
    cin >> a;
    cout << "Enter width: ";
    cin >> b;


    if(a < 0 || ( b < 0) )
    {
        cout << endl<< "Invalid input (from two parameter)! Setting default value to all the field" << endl;
        length = 1;
        width = 1;
    }
    else {
        length = a;
        width = b;
    }
}

double Rectangle::Area()
{
    return length * width;
}

void Rectangle::display()
{

    cout << endl << "Area: " << Area();
}


Rectangle::~Rectangle()
{
    cout << endl << "Clearing memory for: " << length << endl;
}


int main()
{
    Rectangle s1;
    Rectangle s2(0);
    Rectangle s3( 0, 0.0);
    s1.display();
    s2.display();
    s3.display();

    return 0;
}

