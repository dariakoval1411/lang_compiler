@.int_format = private constant [4 x i8] c"%d\0A\00"
@.real_format = private constant [4 x i8] c"%f\0A\00"
@.int_read_format = private constant [3 x i8] c"%d\00"
@.real_read_format = private constant [4 x i8] c"%lf\00"

declare i32 @printf(i8*, ...)
declare i32 @scanf(i8*, ...)

@counter = global i32 5

define i32 @increase(i32 %arg.value) {
entry:
  %value.1 = alloca i32
  store i32 %arg.value, i32* %value.1
  %r2 = load i32, i32* @counter
  %r3 = load i32, i32* %value.1
 %r4 = add i32 %r2, %r3
  store i32 %r4, i32* @counter
  %r5 = load i32, i32* @counter
  ret i32 %r5
}

define i32 @main() {
entry:
  %x.6 = alloca i32
  store i32 10, i32* %x.6
  %x.7 = alloca i32
  store i32 20, i32* %x.7
  %r8 = load i32, i32* %x.7
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r8)
  %r9 = load i32, i32* %x.6
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r9)
  %r10 = load i32, i32* @counter
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r10)
  %r11 = call i32 @increase(i32 3)
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r11)
  %r12 = load i32, i32* @counter
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r12)
  ret i32 0
}
