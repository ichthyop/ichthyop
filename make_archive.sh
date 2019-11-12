dirin=ichthyop_3.3.3

mkdir -p ${dirin}
cp -prfv input ${dirin}
cp -prfv dist/*.jar ${dirin}
cp -prfv dist/lib ${dirin}
cp -prfv cfg ${dirin}
chmod 755 ${dirin}/dist/*.jar

zip -r ${dirin}.zip ${dirin}
rm -rfv ${dirin}
