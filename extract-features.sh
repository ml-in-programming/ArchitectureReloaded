if [ $# -ne "3" ]; then
    echo "usage: features-extraction <path to project> <path to correct refactorings> <path to output folder>"
    exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )" # from https://stackoverflow.com/a/246128

$DIR/gradlew -p $DIR runConsole -PpathToProject="$PWD/$1" -PpathToRefactorings="$PWD/$2" -PpathToFeatures="$PWD/$3"

