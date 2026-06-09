@.int_format = private constant [4 x i8] c"%d\0A\00"
@.real_format = private constant [4 x i8] c"%f\0A\00"
@.int_read_format = private constant [3 x i8] c"%d\00"
@.real_read_format = private constant [4 x i8] c"%lf\00"

declare i32 @printf(i8*, ...)
declare i32 @scanf(i8*, ...)

define i32 @main() {
entry:
  %a = alloca double
  %1 = sitofp i32 5 to double
 %2 = fadd double %1, 2.5
  store double %2, double* %a
  %3 = load double, double* %a
  call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.real_format, i32 0, i32 0), double %3)
  ret i32 0
}
