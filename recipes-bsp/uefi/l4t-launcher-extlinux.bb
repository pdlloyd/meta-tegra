DESCRIPTION = "Generate an extlinux.conf for use with L4TLauncher UEFI application"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

COMPATIBLE_MACHINE = "(tegra)"

DEPENDS = "tegra-flashtools-native dtc-native"

inherit l4t-extlinux-config kernel-artifact-names

KERNEL_ARGS ??= ""
DTBFILE ?= "${@os.path.basename(d.getVar('KERNEL_DEVICETREE').split()[0])}"

# Need to handle:
#  a) Kernel with no initrd/initramfs
#  b) Kernel with bundled initramfs
#  c) Kernel with separate initrd
def compute_dependencies(d):
    deps = "virtual/kernel:do_deploy"
    initramfs_image = d.getVar('INITRAMFS_IMAGE') or ''
    if initramfs_image != '' and (d.getVar('INITRAMFS_IMAGE_BUNDLE') or '') != '1':
        deps += " %s:do_image_complete" % initramfs_image
    return deps


PATH =. "${STAGING_BINDIR_NATIVE}/tegra-flash:"

do_configure() {
    :
}

do_compile() {
    if [ -n "${INITRAMFS_IMAGE}" ]; then
        if [ "${INITRAMFS_IMAGE_BUNDLE}" = "1" ]; then
	    cp -L ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE}-${INITRAMFS_LINK_NAME}.bin ${B}/${KERNEL_IMAGETYPE}
	else
	    cp -L ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE}-${MACHINE}.bin ${B}/${KERNEL_IMAGETYPE}
	    cp -L ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE}-${MACHINE}.cpio.gz ${B}/initrd
	fi
    else
	cp -L ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE}-${MACHINE}.bin ${B}/${KERNEL_IMAGETYPE}
    fi
    if [ -n "${UBOOT_EXTLINUX_FDT}" ]; then
        cp -L ${DEPLOY_DIR_IMAGE}/${DTBFILE} ${B}/
    fi
}
do_compile[depends] += "${@compute_dependencies(d)}"
do_compile[cleandirs] = "${B}"

do_install() {
    install -d ${D}/boot/extlinux ${D}/boot/efi
    install -m 0644 ${B}/${KERNEL_IMAGETYPE} ${D}/boot/
    if [ -n "${UBOOT_EXTLINUX_FDT}" ]; then
        install -m 0644 ${B}/${DTBFILE} ${D}/boot/
    fi
    if [ -n "${INITRAMFS_IMAGE}" -a "${INITRAMFS_IMAGE_BUNDLE}" != "1" ]; then
        install -m 0644 ${B}/initrd ${D}/boot/
    fi
    install -m 0644 ${B}/extlinux.conf ${D}/boot/extlinux/
}

do_install:tegra234() {
    install -d ${D}/boot/extlinux
    install -m 0644 ${B}/${KERNEL_IMAGETYPE} ${D}/boot/
    if [ -n "${UBOOT_EXTLINUX_FDT}" ]; then
        install -m 0644 ${B}/${DTBFILE} ${D}/boot/
    fi
    if [ -n "${INITRAMFS_IMAGE}" -a "${INITRAMFS_IMAGE_BUNDLE}" != "1" ]; then
        install -m 0644 ${B}/initrd ${D}/boot/
    fi
    install -m 0644 ${B}/extlinux.conf ${D}/boot/extlinux/
}

FILES:${PN} = "/boot"
PACKAGE_ARCH = "${MACHINE_ARCH}"
