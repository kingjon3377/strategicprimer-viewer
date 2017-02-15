import controller.map.drivers {
    ISPDriver,
    SPOptions,
    DriverFailedException
}
import java.lang {
    IllegalStateException
}
import model.misc {
    IDriverModel
}
import controller.map.misc {
    ICLIHelper
}
"""An interface to allow utility drivers, which operate on files rather than a map model,
   to be a "functional" (single-method-to-implement) interface"""
interface UtilityDriver satisfies ISPDriver {
    shared actual void startDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
        throw DriverFailedException(IllegalStateException(
            "A utility driver can't operate on a driver model"));
    }
}