traces <- read.csv(paste0(getwd(), "/tracesAnalysis/src/output_data/all_traces.csv"), sep = ";")

snaps <- prcomp(traces[, 1:2], scale = F, center = T)
plot(snaps$x[, 1], snaps$x[, 2], cex = 1, pch = 21, main = "Componentes", ylab = "PC2", xlab = "PC1", bg=1:14[snaps$], each = 10)


text(snaps$x[, 1], snaps$x[, 2] - 0.25, col = "black",cex=0.5)

outliersOut <- traces[which(traces[, 2] <= snaps$sdev[2]),]
snaps2 <- prcomp(outliersOut[, 1:2], scale = F, center = T)
#plot(snaps2$x[, 1], snaps2$x[, 2], cex = 2, pch = 21, bg = "darkorchid", main = "Componentes", ylab = "PC2", xlab = "PC1")
#text(snaps2$x[, 1], snaps2$x[, 2] - 0.25, col = "black")


