Add-Type -AssemblyName System.Drawing
$bmp = New-Object System.Drawing.Bitmap(240,200)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAlias
$g.Clear([System.Drawing.Color]::White)

$darkBlue = [System.Drawing.Color]::FromArgb(44,62,80)
$teal = [System.Drawing.Color]::FromArgb(46,204,155)

$brushDark = New-Object System.Drawing.SolidBrush($darkBlue)
$brushTeal = New-Object System.Drawing.SolidBrush($teal)
$brushWhite = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)

# Draw shield/circle
$g.FillEllipse($brushDark, 70, 10, 100, 100)

# Draw BC text
$fontBC = New-Object System.Drawing.Font("Segoe UI", 32, [System.Drawing.FontStyle]::Bold)
$sf = New-Object System.Drawing.StringFormat
$sf.Alignment = [System.Drawing.StringAlignment]::Center
$sf.LineAlignment = [System.Drawing.StringAlignment]::Center
$rect = New-Object System.Drawing.RectangleF(70, 10, 100, 100)
$g.DrawString("BC", $fontBC, $brushWhite, $rect, $sf)

# Draw gear accents
$g.FillEllipse($brushTeal, 130, 70, 30, 30)
$g.FillEllipse($brushTeal, 80, 15, 25, 25)

# Draw Bizcore text
$fontName = New-Object System.Drawing.Font("Segoe UI", 18, [System.Drawing.FontStyle]::Bold)
$rectName = New-Object System.Drawing.RectangleF(0, 120, 240, 40)
$g.DrawString("Bizcore", $fontName, $brushDark, $rectName, $sf)

# Draw teal underline
$penTeal = New-Object System.Drawing.Pen($teal, 3)
$g.DrawLine($penTeal, 80, 165, 160, 165)

$g.Dispose()

$outputPath = "c:\Users\islem\IdeaProjects\GestionCoach\src\main\resources\edu\Connexion3A7\Controller\images\logo.png"
$bmp.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()
Write-Output "Logo created at $outputPath"
