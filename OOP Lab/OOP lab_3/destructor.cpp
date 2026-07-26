#include<iostream>
using namespace std;

class Myclass{
    int a;

public:
    Myclass();
    void show();
    ~Myclass();
};

Myclass::Myclass(){
    cout << "In constructor!" << endl;
    a = 10;
}

void Myclass::show(){
    cout << "Default value from constructor: " << a << endl;
}

Myclass::~Myclass(){
    cout<<"destructor called " << endl;
}

int main(){
    Myclass o1, o2;
    o1.show();

    return 0;
}
