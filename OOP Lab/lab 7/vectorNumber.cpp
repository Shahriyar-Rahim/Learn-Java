#include<iostream>
using namespace std;

class cp
{
    double x;
    double y;
    double z;

public:
    cp (double r = 0.0, double i = 0.0, double p = 0.0) : x(r), y(i), z(p) {}

    cp operator-(const cp& obj)
    {
        cp temp;
        temp.x = x - obj.x;
        temp.y = y - obj.y;
        temp.z = z - obj.z;

        return temp;
    }

    void display()
    {
        cout << "Vector: " << x << "i^";

        if (y >= 0) cout << " + " << y << "j^";
        else cout << " - " << -y << "j^";

        if (z >= 0) cout << " + " << z << "k^";
        else cout << " - " << -z << "k^";

        cout << endl;
    }
};

int main()
{
    cp c1(5, 3, 4), c2(2, 2, 5), c3;

    c3 = c1 - c2;

    c3.display();

    return 0;
}
