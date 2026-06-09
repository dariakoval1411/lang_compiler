@.int_format = private constant [4 x i8] c"%d\0A\00"
@.real_format = private constant [4 x i8] c"%f\0A\00"
@.int_read_format = private constant [3 x i8] c"%d\00"
@.real_read_format = private constant [4 x i8] c"%lf\00"

declare i32 @printf(i8*, ...)
declare i32 @scanf(i8*, ...)

@x = global i32 100

define i32 @calculate(i32 %arg.x) {
entry:
  %x.1 = alloca i32
  store i32 %arg.x, i32* %x.1
  %result.2 = alloca i32
  %r3 = load i32, i32* %x.1
 %r4 = add i32 %r3, 5
  store i32 %r4, i32* %result.2
  %r5 = load i32, i32* %result.2
  ret i32 %r5
}

define i32 @main() {
entry:
  %r6 = load i32, i32* @x
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r6)
  %r7 = call i32 @calculate(i32 10)
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r7)
  %r8 = load i32, i32* @x
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r8)
  ret i32 0
}
