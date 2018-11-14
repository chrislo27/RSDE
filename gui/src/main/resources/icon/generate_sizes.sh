render() {
	SIZE=$1
	inkscape -z -f logo.svg --export-png $SIZE.png -w $SIZE -h $SIZE
}

render 128
render 256
render 512
render 1024