#include<iostream>
using namespace std;

class Rectengle{
private:
    float lenght, width, area;

public:
    void getInput(){
        cout << "Enter lenght: ";
        cin >> lenght;
        cout << "Enter width: ";
        cin >> width;
    }

    float calcArea() {
        return area = (lenght * width);
    }

    void showArea() {
        cout << "Area: " << lenght << " * " << width << " : " << area << endl;
    }
};

int main() {
    Rectengle r1;
    r1.getInput();
    r1.calcArea();
    r1.showArea();

    return 0;
}
