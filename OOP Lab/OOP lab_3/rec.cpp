#include <iostream>
using namespace std;

class Rectangle {
private:
    float length, width, area;

public:
    void getInput() {
        cout << "Enter length: ";
        cin >> length;
        cout << "Enter width: ";
        cin >> width;
    }
    float calcArea() {
        return area = (length * width);
    }
    void showArea() {
        cout << "Area: " << length << " * " << width << " = " << area << endl;
    }
};

int main() {
    Rectangle r1;
    r1.getInput();
    r1.calcArea();
    r1.showArea();
    return 0;
}
