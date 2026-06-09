%struct.Calculator = type { i32 }
%struct.Counter = type { i32 }

@.int_format = private constant [4 x i8] c"%d\0A\00"
@.real_format = private constant [4 x i8] c"%f\0A\00"
@.int_read_format = private constant [3 x i8] c"%d\00"
@.real_read_format = private constant [4 x i8] c"%lf\00"

declare i32 @printf(i8*, ...)
declare i32 @scanf(i8*, ...)

define i32 @Calculator.add(%struct.Calculator* %this, i32 %arg.value) {
entry:
  %value.1 = alloca i32
  store i32 %arg.value, i32* %value.1
  %r2 = getelementptr %struct.Calculator, %struct.Calculator* %this, i32 0, i32 0
  %r3 = load i32, i32* %r2
  %r4 = load i32, i32* %value.1
 %r5 = add i32 %r3, %r4
  ret i32 %r5
}

define i32 @Counter.get(%struct.Counter* %this) {
entry:
  %r6 = getelementptr %struct.Counter, %struct.Counter* %this, i32 0, i32 0
  %r7 = load i32, i32* %r6
  ret i32 %r7
}

define i32 @Counter.add(%struct.Counter* %this, i32 %arg.amount) {
entry:
  %amount.8 = alloca i32
  store i32 %arg.amount, i32* %amount.8
  %r9 = getelementptr %struct.Counter, %struct.Counter* %this, i32 0, i32 0
  %r10 = getelementptr %struct.Counter, %struct.Counter* %this, i32 0, i32 0
  %r11 = load i32, i32* %r10
  %r12 = load i32, i32* %amount.8
 %r13 = add i32 %r11, %r12
  store i32 %r13, i32* %r9
  %r14 = getelementptr %struct.Counter, %struct.Counter* %this, i32 0, i32 0
  %r15 = load i32, i32* %r14
  ret i32 %r15
}


define i32 @main() {
entry:
  %calculator.16 = alloca %struct.Calculator
  %counter.17 = alloca %struct.Counter
  %r18 = getelementptr %struct.Calculator, %struct.Calculator* %calculator.16, i32 0, i32 0
  store i32 10, i32* %r18
  %r19 = getelementptr %struct.Counter, %struct.Counter* %counter.17, i32 0, i32 0
  store i32 5, i32* %r19
  %r20 = call i32 @Calculator.add(%struct.Calculator* %calculator.16, i32 5)
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r20)
  %r21 = call i32 @Counter.get(%struct.Counter* %counter.17)
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r21)
  %r22 = call i32 @Counter.add(%struct.Counter* %counter.17, i32 3)
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r22)
  %r23 = call i32 @Counter.get(%struct.Counter* %counter.17)
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.int_format, i32 0, i32 0), i32 %r23)
  ret i32 0
}
