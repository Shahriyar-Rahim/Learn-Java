#include<iostream>
using namespace std;

class Rectenglee{
private:
    float length, width, area;

public:
    Rectenglee(float l,float w) {
        if(l < 0) {
            cout<<"Invalid length, setting to 0" << endl;
            length = 0;
        } else {
            length = l;
        }

        if(w < 0) {
            cout<<"Invalid length, setting to 0" << endl;
            width = 0;
        } else {
            width = w;
        }

        calcArea();
    }

    void getData(float l, float w) {

    }

    void calcArea(){
        area = length * width;
    }
    void showArea(){
        cout << "Area: " << length << " * " << width << " : " << area << endl;
    }
};

int main(){
    float l, w;
     cout << "Enter lenght: ";
        cin >> l;
        cout << "Enter width: ";
        cin >> w;
    Rectenglee r1(l, w);
    r1.showArea();

    return 0;
}
