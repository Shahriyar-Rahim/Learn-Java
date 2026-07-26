#include<iostream>
using namespace std;

class cp
{
public:
    double real;
    double img;
    cp(double x, double y);
    cp operator-(const cp& obj);
    void display();
};

cp::cp (double r = 0.0, double i = 0.0) : real(r), img(i) {}
cp cp::operator-(const cp& obj)
    {
        cp temp;
        temp.real = real - obj.real;
        temp.img = img - obj.img;

        return temp;
    }

void cp:: display()
    {
        if(img >= 0)
        {
            cout << "Real + Imaginary: " << real << " + " << img <<"i" << endl;
        }
        else
        {
            cout << "Real + Imaginary: " << real << " - " << -img <<"i" << endl;
        }
    }

class vc
{
    double x;
    double y;
    double z;

public:
    vc(double n, double m, double k);
    void display();
    vc operator-(const vc& obj);
};

vc:: vc (double r = 0.0, double i = 0.0, double p = 0.0) : x(r), y(i), z(p) {}

vc vc:: operator-(const vc& obj)
    {
        vc temp;
        temp.x = x - obj.x;
        temp.y = y - obj.y;
        temp.z = z - obj.z;

        return temp;
    }

void vc:: display()
    {
        cout << "Vector: " << x << "i^";

        if (y >= 0) cout << " + " << y << "j^";
        else cout << " - " << -y << "j^";

        if (z >= 0) cout << " + " << z << "k^";
        else cout << " - " << -z << "k^";

        cout << endl;
    }

int main()
{
    cp c1(5, 3), c2(2, 4), c3;

    vc d1(5, 3, 4), d2(2, 2, 5), d3;

    c3 = c1 - c2;
    d3 = d1 - d2;


    c3.display();
    d3.display();

    return 0;
}
