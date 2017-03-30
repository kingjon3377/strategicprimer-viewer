import model.map {
    HasImage
}
"An interface for model elements that have images that can be used to represent them and
 that can be changed."
shared interface HasMutableImage satisfies HasImage {
	"The per-instance icon filename, now `variable`."
	shared actual formal variable String image;
}