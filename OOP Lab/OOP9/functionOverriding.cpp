#include <iostream>
using namespace std;

class Shape {
public:
    // Base class virtual functions no longer need parameters
    virtual void area() {
        cout << "Dummy Area of the shape" << endl;
    }

    virtual void circumference() {
        cout << "Dummy circumference of the shape" << endl;
    }
};

class Rectangle : public Shape {
private:
    double length, width;
public:
    // Constructor to initialize values
    Rectangle(double l, double w) {
        length = l;
        width = w;
    }

    void area() override {
        double res = length * width;
        cout << "Area of the Rectangle: " << res << endl;
    }

    void circumference() override {
        double res = 2 * (length + width);
        cout << "Circumference of the Rectangle: " << res << endl;
    }
};

class Triangle : public Shape {
private:
    double base, height, s1, s2, s3;
public:
    // Constructor for Triangle
    Triangle(double b, double h, double side1, double side2, double side3) {
        base = b;
        height = h;
        s1 = side1;
        s2 = side2;
        s3 = side3;
    }

    void area() override {
        double res = 0.5 * base * height;
        cout << "Area of the Triangle: " << res << endl;
    }

    void circumference() override {
        double res = s1 + s2 + s3;
        cout << "Circumference of the Triangle: " << res << endl;
    }
};

class Circle : public Shape {
private:
    double radius;
public:
    // Constructor for Circle
    Circle(double r) {
        radius = r;
    }

    void area() override {
        double res = 3.1416 * radius * radius;
        cout << "Area of the Circle: " << res << endl;
    }

    void circumference() override {
        double res = 2 * 3.1416 * radius;
        cout << "Circumference of the Circle: " << res << endl;
    }
};

int main() {
    Shape* s;

    // Values are passed ONLY here, at the moment of creation
    Rectangle r(5, 2);
    Triangle t(4, 5, 3, 4, 5);
    Circle c(3);

    // Using the pointer to call the methods
    cout << "--- Rectangle Stats ---" << endl;
    s = &r;
    s->area();
    s->circumference();

    cout << "\n--- Triangle Stats ---" << endl;
    s = &t;
    s->area();
    s->circumference();

    cout << "\n--- Circle Stats ---" << endl;
    s = &c;
    s->area();
    s->circumference();

    return 0;
}
