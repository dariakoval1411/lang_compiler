# Dokumentacja języka Nexa

## Spis treści

- [Dokumentacja języka Nexa](#dokumentacja-języka-nexa)
  - [Spis treści](#spis-treści)
  - [1. Opis języka](#1-opis-języka)
  - [2. Struktura programu](#2-struktura-programu)
  - [3. Typy danych](#3-typy-danych)
  - [4. Zmienne globalne i lokalne](#4-zmienne-globalne-i-lokalne)
  - [5. Instrukcje warunkowe](#5-instrukcje-warunkowe)
  - [6. Pętle](#6-pętle)
  - [7. Funkcje](#7-funkcje)
  - [8. Tablice](#8-tablice)
  - [9. Struktury](#9-struktury)
  - [10. Klasy](#10-klasy)
  - [11. Wejście i wyjście](#11-wejście-i-wyjście)
  - [12. Błędy semantyczne](#12-błędy-semantyczne)
  - [13. Ograniczenia języka](#13-ograniczenia-języka)
  - [14. Przykładowy program](#14-przykładowy-program)

## 1. Opis języka

Nexa jest prostym językiem programowania kompilowanym do LLVM IR. Język obsługuje zmienne, wyrażenia arytmetyczne, instrukcje warunkowe, pętle, funkcje, tablice, struktury oraz klasy.

Język został zaprojektowany jako język edukacyjny o prostej i czytelnej składni.

## 2. Struktura programu

Każdy program musi zawierać blok główny rozpoczynający się od słowa kluczowego `program` i kończący się słowem `end`.

```text
program Main

show(1);

end
```

Przed blokiem `program` można deklarować zmienne globalne, funkcje, struktury oraz klasy.

```text
int globalValue = 10;

function int add(int a, int b) {
    return a + b;
}

program Main

show(add(globalValue, 5));

end
```

## 3. Typy danych

Język obsługuje dwa podstawowe typy danych:

| Typ | Opis |
|---|---|
| `int` | liczby całkowite |
| `real` | liczby rzeczywiste |

Przykład:

```text
program Main

int x = 10;
real y = 4.5;

show(x);
show(y);

end
```

Wartość typu `int` może zostać użyta tam, gdzie oczekiwany jest typ `real`.

## 4. Zmienne globalne i lokalne

Zmienne globalne deklaruje się przed blokiem `program`.

```text
int counter = 5;

program Main

show(counter);

end
```

Zmienne lokalne można deklarować wewnątrz programu, funkcji oraz bloków `{ ... }`.

```text
program Main

int x = 10;

{
    int x = 20;
    show(x);
}

show(x);

end
```

Oczekiwany wynik:

```text
20
10
```

Zmienne lokalne działają tylko w swoim zakresie. Możliwe jest przesłanianie zmiennych zewnętrznych.

## 5. Instrukcje warunkowe

Instrukcja warunkowa ma postać:

```text
if (warunek) {
    instrukcje
}
```

Można także użyć części `else`.

```text
if (warunek) {
    instrukcje
} else {
    instrukcje
}
```

Przykład:

```text
program Main

int x = 7;

if (x > 5) {
    show(1);
} else {
    show(0);
}

end
```

Oczekiwany wynik:

```text
1
```

Obsługiwane operatory porównania:

| Operator | Znaczenie |
|---|---|
| `<` | mniejsze niż |
| `>` | większe niż |
| `<=` | mniejsze lub równe |
| `>=` | większe lub równe |
| `==` | równe |
| `!=` | różne |

## 6. Pętle

Język obsługuje pętlę `while`.

```text
program Main

int i = 1;

while (i <= 5) {
    if (i == 3) {
        show(i * 3);
    } else {
        show(i);
    }

    i = i + 1;
}

end
```

Oczekiwany wynik:

```text
1
2
9
4
5
```

Warunek pętli musi być wyrażeniem logicznym, np. porównaniem.

## 7. Funkcje

Funkcje deklaruje się przed blokiem `program`.

```text
function int add(int a, int b) {
    return a + b;
}

program Main

show(add(4, 6));

end
```

Oczekiwany wynik:

```text
10
```

Funkcja może posiadać zmienne lokalne.

```text
function int square(int value) {
    int result = value * value;
    return result;
}

program Main

show(square(5));

end
```

Oczekiwany wynik:

```text
25
```

Funkcje mogą przyjmować i zwracać wartości typu `int` oraz `real`.

## 8. Tablice

Tablice deklaruje się przez podanie typu, nazwy i rozmiaru.

```text
program Main

int numbers[3];

numbers[0] = 1;
numbers[1] = 2;
numbers[2] = 3;

show(numbers[0] + numbers[1] + numbers[2]);

end
```

Oczekiwany wynik:

```text
6
```

Indeksowanie tablic zaczyna się od `0`.

## 9. Struktury

Struktura pozwala grupować kilka pól pod jedną nazwą.

```text
struct Point {
    int x;
    int y;
}

program Main

Point point;

point.x = 3;
point.y = 4;

show(point.x + point.y);

end
```

Oczekiwany wynik:

```text
7
```

Struktury mogą posiadać pola różnych typów.

```text
struct Product {
    int count;
    real price;
}

program Main

Product product;

product.count = 3;
product.price = 4.5;

show(product.count * product.price);

end
```

Oczekiwany wynik:

```text
13.500000
```

## 10. Klasy

Klasy są podobne do struktur, ale mogą posiadać metody.

```text
class Counter {
    int value;

    function int get() {
        return this.value;
    }

    function int add(int amount) {
        this.value = this.value + amount;
        return this.value;
    }
}

program Main

Counter counter;

counter.value = 5;

show(counter.get());
show(counter.add(3));
show(counter.get());

end
```

Oczekiwany wynik:

```text
5
8
8
```

Dostęp do pól i metod obiektu odbywa się przez operator `.`. W metodach można używać słowa kluczowego `this`.

## 11. Wejście i wyjście

Do wypisywania wartości służy instrukcja `show`.

```text
program Main

show(10);
show(4.5);

end
```

Do wczytywania wartości służy instrukcja `input`.

```text
program Main

int x;

input(x);
show(x);

end
```

## 12. Błędy semantyczne

Kompilator sprawdza poprawność semantyczną programu.

Przykład użycia niezadeklarowanej zmiennej:

```text
program Main

show(x);

end
```

Przykładowy błąd:

```text
Semantic Error: Variable not declared: x
```

Przykład błędnego warunku:

```text
program Main

int x = 5;

if (x) {
    show(x);
}

end
```

Przykładowy błąd:

```text
Semantic Error: If condition must be bool
```

Przykład błędnego typu argumentu funkcji:

```text
function int add(int a, int b) {
    return a + b;
}

program Main

show(add(1, 2.5));

end
```

Przykładowy błąd:

```text
Semantic Error: Cannot assign REAL to INT
```

## 13. Ograniczenia języka

Aktualna wersja języka ma następujące ograniczenia:

- obsługiwane są tylko typy `int` i `real`;
- funkcje mogą przyjmować i zwracać tylko wartości typu `int` oraz `real`;
- funkcje nie mogą przyjmować ani zwracać tablic;
- funkcje nie obsługują typu `void`;
- każda funkcja musi kończyć się instrukcją `return`;
- tablice mają stały rozmiar podany przy deklaracji;
- operatory logiczne `&&`, `||`, `!` nie są obsługiwane;
- warunki w instrukcjach `if` i `while` muszą być porównaniami;
- struktury i klasy muszą być zadeklarowane przed użyciem;
- funkcje muszą być zadeklarowane przed blokiem `program`;
- klasy nie obsługują dziedziczenia;
- konstruktory nie są obsługiwane.

## 14. Przykładowy program

```text
struct Student {
    int age;
    real grade;
}

class Counter {
    int value;

    function int get() {
        return this.value;
    }

    function int add(int amount) {
        this.value = this.value + amount;
        return this.value;
    }
}

int globalBonus = 10;

function int add(int a, int b) {
    return a + b;
}

function int sumRange(int start, int finish, int result) {
    while (start <= finish) {
        result = result + start;
        start = start + 1;
    }

    return result;
}

function real multiplyReal(real value, int factor) {
    return value * factor;
}

program Main

int numbers[3];

numbers[0] = 2;
numbers[1] = 4;
numbers[2] = 6;

show(numbers[0]);
show(numbers[1]);
show(numbers[2]);

int sum = numbers[0] + numbers[1] + numbers[2];

show(sum);

if (sum > 10) {
    show(add(sum, globalBonus));
} else {
    show(sum);
}

show(sumRange(1, 5, 0));

Student student;

student.age = 21;
student.grade = 4.5;

show(student.age);
show(student.grade);
show(multiplyReal(student.grade, 2));

Counter counter;

counter.value = 5;

show(counter.get());
show(counter.add(3));
show(counter.get());

{
    int sum = 100;
    show(sum);
}

show(sum);

end
```

Oczekiwany wynik:

```text
2
4
6
12
22
15
21
4.500000
9.000000
5
8
8
100
12
```
