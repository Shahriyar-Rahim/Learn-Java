#include <iostream>
using namespace std;

class Person
{
private:
    string name;
    int age;

public:
    //setter function
    void setData(string n, int a){
        name = n;
        age = a;
    }

    string getName(){
        return name;
    }

    int getAge() {
        return age;
    }

};

int main()
{
    Person p;
    p.setData("Rahim", 21);
    cout << "Name:" << p.getName() << endl;
    cout << "Age: " << p.getAge() << endl;

    return 0;
}
