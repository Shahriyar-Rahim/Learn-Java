#include<iostream>
using namespace std;

class Area
{
    double height, width, area;

public:
    Area(double x = 0.0, double y = 0.0) : height(x), width(y) {}

    void inputData()
    {
        cout << "Input height: ";
        cin >> height;
        cout << "Input Width: ";
        cin >> width;
    }

    void calculate()
    {
        area = width * height;
    }

    void shw()
    {
        cout << "area: " << area;
    }
};

int main()
{
    Area obj;

    obj.inputData();
    obj.calculate();
    obj.shw();

    return 0;
}
