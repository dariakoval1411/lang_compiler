@.int_format = private constant [4 x i8] c"%d\0A\00"
@.real_format = private constant [4 x i8] c"%f\0A\00"
@.int_read_format = private constant [3 x i8] c"%d\00"
@.real_read_format = private constant [4 x i8] c"%lf\00"

declare i32 @printf(i8*, ...)
declare i32 @scanf(i8*, ...)


define i32 @main() {
entry:
  %x.1 = alloca i32
  store i32 15, i32* %x.1
  %r2 = load i32, i32* %x.1
 %r3 = add i32 %r2, 2
  %r4 = icmp slt i32 %r3, 10
  br i1 %r4, label %if.then.1, label %if.else.3
if.then.1:
  %r5 = load i32, i32* %x.1
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r5)
  %r6 = load i32, i32* %x.1
  %r7 = icmp sgt i32 %r6, 5
  br i1 %r7, label %if.then.4, label %if.end.5
if.then.4:
  %r8 = load i32, i32* %x.1
 %r9 = add i32 %r8, 10
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r9)
  br label %if.end.5
if.end.5:
  br label %if.end.2
if.else.3:
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 0)
  br label %if.end.2
if.end.2:
  ret i32 0
}
