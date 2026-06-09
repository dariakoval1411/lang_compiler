@.int_format = private constant [4 x i8] c"%d\0A\00"
@.real_format = private constant [4 x i8] c"%f\0A\00"
@.int_read_format = private constant [3 x i8] c"%d\00"
@.real_read_format = private constant [4 x i8] c"%lf\00"

declare i32 @printf(i8*, ...)
declare i32 @scanf(i8*, ...)


define i32 @main() {
entry:
  %i.1 = alloca i32
  store i32 1, i32* %i.1
  br label %while.condition.1
while.condition.1:
  %r2 = load i32, i32* %i.1
  %r3 = icmp sle i32 %r2, 5
  br i1 %r3, label %while.body.2, label %while.end.3
while.body.2:
  %r4 = load i32, i32* %i.1
  %r5 = icmp eq i32 %r4, 3
  br i1 %r5, label %if.then.4, label %if.else.6
if.then.4:
  %r6 = load i32, i32* %i.1
 %r7 = mul i32 %r6, 10
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r7)
  br label %if.end.5
if.else.6:
  %r8 = load i32, i32* %i.1
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r8)
  br label %if.end.5
if.end.5:
  %r9 = load i32, i32* %i.1
 %r10 = add i32 %r9, 1
  store i32 %r10, i32* %i.1
  br label %while.condition.1
while.end.3:
  ret i32 0
}
