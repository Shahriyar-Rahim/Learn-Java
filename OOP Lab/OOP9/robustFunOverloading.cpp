#include <iostream>

using namespace std;

class Shape {
public:
    virtual void area() const = 0;
    virtual void circumference() const = 0;

    virtual ~Shape() {}
};

class Rectangle : public Shape {
private:
    double length, width;
public:
    Rectangle(double l, double w) : length(l), width(w) {}

    void area() const override {
        cout << "Area of the Rectangle: " << (length * width) << endl;
    }

    void circumference() const override {
        cout << "Circumference of the Rectangle: " << (2 * (length + width)) << endl;
    }
};

class Triangle : public Shape {
private:
    double base, height, s1, s2, s3;
public:
    Triangle(double b, double h, double side1, double side2, double side3)
        : base(b), height(h), s1(side1), s2(side2), s3(side3) {}

    void area() const override {
        cout << "Area of the Triangle: " << (0.5 * base * height) << endl;
    }

    void circumference() const override {
        cout << "Circumference of the Triangle: " << (s1 + s2 + s3) << endl;
    }
};

class Circle : public Shape {
private:
    double radius;
    static constexpr double PI = 3.141592653589793;
public:
    Circle(double r) : radius(r) {}

    void area() const override {
        cout << "Area of the Circle: " << (PI * radius *radius) << endl;
    }

    void circumference() const override {
        cout << "Circumference of the Circle: " << (2 * PI * radius) << endl;
    }
};

int main() {
    Shape* shapes[3];

    // Initializing specific derived objects
    shapes[0] = new Rectangle(5, 2);
    shapes[1] = new Triangle(4, 5, 3, 4, 5);
    shapes[2] = new Circle(3);

    const char* labels[] = {"Rectangle Stats", "Triangle Stats", "Circle Stats"};

    for (int i = 0; i < 3; i++) {
        cout << "--- " << labels[i] << " ---" << endl;
        shapes[i]->area();
        shapes[i]->circumference();
        cout << endl;
    }

    for (int i = 0; i < 3; i++) {
        delete shapes[i];
    }

    return 0;
}
