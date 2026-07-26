#include<iostream>
using namespace std;

template <class T>

class Compare
{
    T a , b;

public:
    Compare(T x, T y) : a(x), b(y) {}
    T Max();
    T Min();

    ~Compare() {}
};

template <class T>
T Compare<T>:: Max()
{
    return (a > b) ? a : b;
}

template <class T>
T Compare<T>:: Min()

{
    return (a < b) ? a : b;
}

int main()
{
    Compare<int> c(3, 2);
    cout << "Max: " << c.Max() << endl;
    cout << "Min: " << c.Min() << endl;

    Compare<float> c2(5.2, 8.9);
    cout << "Max: " << c2.Max() << endl;
    cout << "Min: " << c2.Min() << endl;

    Compare<char> c3('g', 'G');
    cout << "Max: " << c3.Max() << endl;
    cout << "Min: " << c3.Min() << endl;

    Compare<string> c4("Hello", "Hi");
    cout << "Max: " << c4.Max() << endl;
    cout << "Min: " << c4.Min() << endl;

    return 0;
}
