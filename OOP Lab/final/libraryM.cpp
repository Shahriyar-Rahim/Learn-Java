#include <iostream>
#include <vector>
#include <stdexcept>
using namespace std;

class Book{
    string title;
    int id, quantity;

    public:
    Book(string t, int i, int q) : title(t), id(i), quantity(q) {}
}