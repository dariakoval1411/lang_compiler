@.int_format = private constant [4 x i8] c"%d\0A\00"
@.real_format = private constant [4 x i8] c"%f\0A\00"
@.int_read_format = private constant [3 x i8] c"%d\00"
@.real_read_format = private constant [4 x i8] c"%lf\00"

declare i32 @printf(i8*, ...)
declare i32 @scanf(i8*, ...)


define i32 @fun_1() {
entry:
  ret i32 42
}

define double @add(i32 %arg.a, i32 %arg.b) {
entry:
  %a.1 = alloca i32
  store i32 %arg.a, i32* %a.1
  %b.2 = alloca i32
  store i32 %arg.b, i32* %b.2
  %r3 = load i32, i32* %a.1
  %r4 = load i32, i32* %b.2
 %r5 = add i32 %r3, %r4
  %r6 = sitofp i32 %r5 to double
  ret double %r6
}

define i32 @minus(i32 %arg.a, i32 %arg.b) {
entry:
  %a.7 = alloca i32
  store i32 %arg.a, i32* %a.7
  %b.8 = alloca i32
  store i32 %arg.b, i32* %b.8
  %r9 = load i32, i32* %a.7
  %r10 = load i32, i32* %b.8
 %r11 = sub i32 %r9, %r10
  ret i32 %r11
}

define i32 @x_sum(i32 %arg.a, i32 %arg.b, i32 %arg.c) {
entry:
  %a.12 = alloca i32
  store i32 %arg.a, i32* %a.12
  %b.13 = alloca i32
  store i32 %arg.b, i32* %b.13
  %c.14 = alloca i32
  store i32 %arg.c, i32* %c.14
  br label %while.condition.1
while.condition.1:
  %r15 = load i32, i32* %a.12
  %r16 = load i32, i32* %b.13
  %r17 = icmp slt i32 %r15, %r16
  br i1 %r17, label %while.body.2, label %while.end.3
while.body.2:
  %r18 = load i32, i32* %c.14
  %r19 = load i32, i32* %a.12
 %r20 = add i32 %r18, %r19
  store i32 %r20, i32* %c.14
  %r21 = load i32, i32* %a.12
 %r22 = add i32 %r21, 1
  store i32 %r22, i32* %a.12
  br label %while.condition.1
while.end.3:
  %r23 = load i32, i32* %c.14
  ret i32 %r23
}

define i32 @square(i32 %arg.value) {
entry:
  %value.24 = alloca i32
  store i32 %arg.value, i32* %value.24
  %r25 = load i32, i32* %value.24
  %r26 = load i32, i32* %value.24
 %r27 = mul i32 %r25, %r26
  ret i32 %r27
}

define i32 @main() {
entry:
  %y.28 = alloca i32
  %r29 = call i32 @fun_1()
  store i32 %r29, i32* %y.28
  %r30 = load i32, i32* %y.28
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r30)
  %r31 = call double @add(i32 4, i32 6)
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.real_format, i32 0, i32 0), double %r31)
  %r32 = call i32 @minus(i32 6, i32 10)
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r32)
  %r33 = call i32 @x_sum(i32 2, i32 8, i32 0)
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r33)
  %r34 = call i32 @square(i32 4)
  %r35 = call double @add(i32 %r34, i32 3)
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.real_format, i32 0, i32 0), double %r35)
  ret i32 0
}
