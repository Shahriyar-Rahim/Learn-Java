#include<iostream>
using namespace std;

template <class T>
class Complex
{
    T a, b;

public:
    Complex(T x, T y) : a(x), b(y) {}
    T Max();
    T Min();
    T Add();

    ~Complex() {}
};

template <class T>
T Complex<T>:: Max()
{
    return ( a > b ) ? a : b;
}

template <class T>
T Complex<T>:: Min()
{
    return (a < b ) ? a : b;
}

template <class T>
T Complex<T>:: Add()
{
    return a + b;
}

int main()
{
    Complex<int> c(3, 2);
    cout << "Max: " << c.Max() << endl;
    cout << "Min: " << c.Min() << endl;
    cout << "Sum: " << c.Add() << endl;

    Complex<float> c2(5.2, 8.9);
    cout << "Max: " << c2.Max() << endl;
    cout << "Min: " << c2.Min() << endl;
    cout << "Sum: " << c2.Add() << endl;

    Complex<char> c3('g', 'G');
    cout << "Max: " << c3.Max() << endl;
    cout << "Min: " << c3.Min() << endl;
    cout << "Sum: " << c3.Add() << endl;

    Complex<string> c4("Hello", "Hi");
    cout << "Max: " << c4.Max() << endl;
    cout << "Min: " << c4.Min() << endl;
    cout << "Sum: " << c4.Add() << endl;

    return 0;
}
